-- Allow regular users to see their assigned work and create progress records.

create or replace function public.current_user_is_utilizador()
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1
    from public.users u
    where u.id = public.current_user_public_id()
      and upper(u.role) = 'UTILIZADOR'
      and upper(u.status) in ('ATIVO', 'ACTIVO')
  )
$$;

create or replace function public.current_user_is_assigned_to_task(p_task_id integer)
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1
    from public.tarefa_users tu
    where tu.tarefa_id = p_task_id
      and tu.user_id = public.current_user_public_id()
  )
$$;

create or replace function public.current_user_can_access_observation(p_observacao_id integer)
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1
    from public.observacoes o
    inner join public.registos_tarefa rt
      on rt.id = o.registo_id
    where o.id = p_observacao_id
      and rt.user_id = public.current_user_public_id()
  )
$$;

grant execute on function public.current_user_is_utilizador() to authenticated;
grant execute on function public.current_user_is_assigned_to_task(integer) to authenticated;
grant execute on function public.current_user_can_access_observation(integer) to authenticated;

drop policy if exists "Users can read own project memberships" on public.projeto_users;
drop policy if exists "Users can read own task memberships" on public.tarefa_users;
drop policy if exists "Users can read assigned projects" on public.projetos;
drop policy if exists "Users can read assigned tasks" on public.tarefas;
drop policy if exists "Users can complete assigned tasks" on public.tarefas;
drop policy if exists "Users can read own task records" on public.registos_tarefa;
drop policy if exists "Users can create own task records" on public.registos_tarefa;
drop policy if exists "Users can update own task records" on public.registos_tarefa;
drop policy if exists "Users can delete own task records" on public.registos_tarefa;
drop policy if exists "Users can read own observations" on public.observacoes;
drop policy if exists "Users can create own observations" on public.observacoes;
drop policy if exists "Users can update own observations" on public.observacoes;
drop policy if exists "Users can delete own observations" on public.observacoes;
drop policy if exists "Users can read own observation photos" on public.observacao_fotos;
drop policy if exists "Users can create own observation photos" on public.observacao_fotos;
drop policy if exists "Users can update own observation photos" on public.observacao_fotos;
drop policy if exists "Users can delete own observation photos" on public.observacao_fotos;

create policy "Users can read own project memberships"
on public.projeto_users
for select
to authenticated
using (
  public.current_user_is_utilizador()
  and user_id = public.current_user_public_id()
);

create policy "Users can read own task memberships"
on public.tarefa_users
for select
to authenticated
using (
  public.current_user_is_utilizador()
  and user_id = public.current_user_public_id()
);

create policy "Users can read assigned projects"
on public.projetos
for select
to authenticated
using (
  public.current_user_is_utilizador()
  and (
    exists (
      select 1
      from public.projeto_users pu
      where pu.projeto_id = projetos.id
        and pu.user_id = public.current_user_public_id()
    )
    or exists (
      select 1
      from public.tarefa_users tu
      inner join public.tarefas t
        on t.id = tu.tarefa_id
      where t.projeto_id = projetos.id
        and tu.user_id = public.current_user_public_id()
    )
  )
);

create policy "Users can read assigned tasks"
on public.tarefas
for select
to authenticated
using (
  public.current_user_is_utilizador()
  and public.current_user_is_assigned_to_task(id)
);

create policy "Users can complete assigned tasks"
on public.tarefas
for update
to authenticated
using (
  public.current_user_is_utilizador()
  and public.current_user_is_assigned_to_task(id)
)
with check (
  public.current_user_is_utilizador()
  and public.current_user_is_assigned_to_task(id)
);

create policy "Users can read own task records"
on public.registos_tarefa
for select
to authenticated
using (
  public.current_user_is_utilizador()
  and user_id = public.current_user_public_id()
);

create policy "Users can create own task records"
on public.registos_tarefa
for insert
to authenticated
with check (
  public.current_user_is_utilizador()
  and user_id = public.current_user_public_id()
  and public.current_user_is_assigned_to_task(tarefa_id)
);

create policy "Users can update own task records"
on public.registos_tarefa
for update
to authenticated
using (
  public.current_user_is_utilizador()
  and user_id = public.current_user_public_id()
)
with check (
  public.current_user_is_utilizador()
  and user_id = public.current_user_public_id()
  and public.current_user_is_assigned_to_task(tarefa_id)
);

create policy "Users can delete own task records"
on public.registos_tarefa
for delete
to authenticated
using (
  public.current_user_is_utilizador()
  and user_id = public.current_user_public_id()
);

create policy "Users can read own observations"
on public.observacoes
for select
to authenticated
using (
  public.current_user_is_utilizador()
  and exists (
    select 1
    from public.registos_tarefa rt
    where rt.id = observacoes.registo_id
      and rt.user_id = public.current_user_public_id()
  )
);

create policy "Users can create own observations"
on public.observacoes
for insert
to authenticated
with check (
  public.current_user_is_utilizador()
  and exists (
    select 1
    from public.registos_tarefa rt
    where rt.id = observacoes.registo_id
      and rt.user_id = public.current_user_public_id()
  )
);

create policy "Users can update own observations"
on public.observacoes
for update
to authenticated
using (
  public.current_user_is_utilizador()
  and exists (
    select 1
    from public.registos_tarefa rt
    where rt.id = observacoes.registo_id
      and rt.user_id = public.current_user_public_id()
  )
)
with check (
  public.current_user_is_utilizador()
  and exists (
    select 1
    from public.registos_tarefa rt
    where rt.id = observacoes.registo_id
      and rt.user_id = public.current_user_public_id()
  )
);

create policy "Users can delete own observations"
on public.observacoes
for delete
to authenticated
using (
  public.current_user_is_utilizador()
  and exists (
    select 1
    from public.registos_tarefa rt
    where rt.id = observacoes.registo_id
      and rt.user_id = public.current_user_public_id()
  )
);

create policy "Users can read own observation photos"
on public.observacao_fotos
for select
to authenticated
using (
  public.current_user_is_utilizador()
  and public.current_user_can_access_observation(observacao_id)
);

create policy "Users can create own observation photos"
on public.observacao_fotos
for insert
to authenticated
with check (
  public.current_user_is_utilizador()
  and public.current_user_can_access_observation(observacao_id)
);

create policy "Users can update own observation photos"
on public.observacao_fotos
for update
to authenticated
using (
  public.current_user_is_utilizador()
  and public.current_user_can_access_observation(observacao_id)
)
with check (
  public.current_user_is_utilizador()
  and public.current_user_can_access_observation(observacao_id)
);

create policy "Users can delete own observation photos"
on public.observacao_fotos
for delete
to authenticated
using (
  public.current_user_is_utilizador()
  and public.current_user_can_access_observation(observacao_id)
);
