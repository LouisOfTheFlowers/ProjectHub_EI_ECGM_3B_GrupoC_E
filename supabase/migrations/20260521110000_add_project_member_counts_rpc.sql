-- Return project member counts through a security definer RPC for reports.

create or replace function public.get_project_member_counts()
returns table (
  projeto_id integer,
  membros bigint
)
language sql
security definer
set search_path = public
stable
as $$
  select
    pu.projeto_id,
    count(*)::bigint as membros
  from public.projeto_users pu
  where public.current_user_is_admin()
  group by pu.projeto_id
$$;

revoke all on function public.get_project_member_counts() from public;
grant execute on function public.get_project_member_counts() to authenticated;
